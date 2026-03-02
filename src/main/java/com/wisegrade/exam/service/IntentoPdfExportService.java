package com.wisegrade.exam.service;

import com.wisegrade.academic.model.Estudiante;
import com.wisegrade.academic.repository.EstudianteRepository;
import com.wisegrade.auth.security.AuthPrincipal;
import com.wisegrade.common.BadRequestException;
import com.wisegrade.exam.api.dto.CorreccionPreguntaResponse;
import com.wisegrade.exam.api.dto.IntentoDetalleResponse;
import com.wisegrade.exam.api.dto.PreguntaGeneratedResponse;
import com.wisegrade.exam.model.IntentoEstado;
import com.wisegrade.exam.model.RespuestaCorrecta;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class IntentoPdfExportService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT);

    private final IntentoExamenService intentoExamenService;
    private final EstudianteRepository estudianteRepository;

    public IntentoPdfExportService(
            IntentoExamenService intentoExamenService,
            EstudianteRepository estudianteRepository) {
        this.intentoExamenService = intentoExamenService;
        this.estudianteRepository = estudianteRepository;
    }

    public byte[] exportSubmittedIntentoPdf(AuthPrincipal principal, long intentoId) {
        IntentoDetalleResponse detalle = intentoExamenService.getDetalle(principal, intentoId);
        if (detalle.estado() != IntentoEstado.SUBMITTED) {
            throw new BadRequestException("Solo se puede exportar PDF cuando el intento está SUBMITTED");
        }

        Estudiante estudiante = estudianteRepository.findById(detalle.estudianteId()).orElse(null);

        Map<Long, CorreccionPreguntaResponse> correccionByPreguntaId = new HashMap<>();
        if (detalle.correccion() != null) {
            for (CorreccionPreguntaResponse c : detalle.correccion()) {
                if (c == null)
                    continue;
                correccionByPreguntaId.put(c.preguntaId(), c);
            }
        }

        try (PDDocument doc = new PDDocument()) {
            PdfCursor cursor = new PdfCursor(doc);

            cursor.h1("WiseGrade - Examen");

            String estudianteLine = estudiante == null
                    ? ("EstudianteId: " + detalle.estudianteId())
                    : ("Estudiante: " + estudiante.getNombres() + " " + estudiante.getApellidos()
                            + " (" + estudiante.getDocumento() + ")");

            cursor.p("IntentoId: " + detalle.intentoId());
            cursor.p("ExamenId: " + detalle.examenId());
            cursor.p(estudianteLine);
            cursor.p("Inicio: " + (detalle.startedAt() == null ? "" : DT.format(detalle.startedAt())));
            cursor.p("Envío: " + (detalle.submittedAt() == null ? "" : DT.format(detalle.submittedAt())));

            if (detalle.resultado() != null) {
                cursor.p("Resultado: " + detalle.resultado().correctas() + "/" + detalle.resultado().total()
                        + " - Nota: " + detalle.resultado().notaSobre5());
            }

            cursor.blank(8);

            List<PreguntaGeneratedResponse> preguntas = detalle.preguntas() == null ? List.of() : detalle.preguntas();
            for (int i = 0; i < preguntas.size(); i++) {
                PreguntaGeneratedResponse p = preguntas.get(i);
                if (p == null)
                    continue;
                CorreccionPreguntaResponse c = correccionByPreguntaId.get(p.id());

                cursor.h2((i + 1) + ". " + safe(p.enunciado()));

                List<String> opciones = p.opciones();
                cursor.p("A. " + safe(opt(opciones, 0)));
                cursor.p("B. " + safe(opt(opciones, 1)));
                cursor.p("C. " + safe(opt(opciones, 2)));
                cursor.p("D. " + safe(opt(opciones, 3)));

                RespuestaCorrecta respEst = c == null ? null : c.respuestaEstudiante();
                RespuestaCorrecta respCor = c == null ? null : c.respuestaCorrecta();

                cursor.p("Tu respuesta: " + (respEst == null ? "Sin responder" : respEst.name()));
                cursor.p("Correcta: " + (respCor == null ? "" : respCor.name()));
                if (c != null) {
                    cursor.p("Resultado: " + (c.esCorrecta() ? "Correcta" : "Incorrecta"));
                    if (c.explicacion() != null && !c.explicacion().isBlank()) {
                        cursor.p("Explicación: " + safe(c.explicacion()));
                    }
                }

                cursor.blank(10);
            }

            cursor.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el PDF", e);
        }
    }

    private static String opt(List<String> opciones, int idx) {
        if (opciones == null || idx < 0 || idx >= opciones.size())
            return "";
        String v = opciones.get(idx);
        return v == null ? "" : v;
    }

    private static String safe(String s) {
        if (s == null)
            return "";
        // Basic cleanup for PDF text operators.
        return s
                .replace("\r", " ")
                .replace("\n", " ")
                .replace('\u00A0', ' ')
                .trim();
    }

    private static final class PdfCursor implements AutoCloseable {
        private static final PDRectangle PAGE_SIZE = PDRectangle.A4;
        private static final float MARGIN = 50f;
        private static final float FONT_SIZE = 11f;
        private static final float LEADING = 14f;

        private static final PDFont FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private static final PDFont FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        private final PDDocument doc;
        private PDPage page;
        private PDPageContentStream cs;
        private float y;

        PdfCursor(PDDocument doc) throws IOException {
            this.doc = doc;
            newPage();
        }

        void h1(String text) throws IOException {
            writeWrapped(text, FONT_BOLD, 16f, 20f);
            blank(6);
        }

        void h2(String text) throws IOException {
            writeWrapped(text, FONT_BOLD, 12.5f, 16f);
        }

        void p(String text) throws IOException {
            writeWrapped(text, FONT_REGULAR, FONT_SIZE, LEADING);
        }

        void blank(float pts) {
            y -= pts;
        }

        private void ensureSpace(float required) throws IOException {
            if (y - required < MARGIN) {
                closeStream();
                newPage();
            }
        }

        private void newPage() throws IOException {
            page = new PDPage(PAGE_SIZE);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            y = PAGE_SIZE.getHeight() - MARGIN;
        }

        private void closeStream() throws IOException {
            if (cs != null) {
                cs.close();
                cs = null;
            }
        }

        @Override
        public void close() throws IOException {
            closeStream();
        }

        private void writeWrapped(String rawText, PDFont font, float fontSize, float leading) throws IOException {
            String text = makeEncodable(rawText, font);
            float width = PAGE_SIZE.getWidth() - 2 * MARGIN;

            // Very small paragraphs still require a line.
            ensureSpace(leading + 2);

            for (String line : wrapLine(text, font, fontSize, width)) {
                ensureSpace(leading + 2);
                cs.beginText();
                cs.setFont(font, fontSize);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText(line);
                cs.endText();
                y -= leading;
            }
        }

        private static String makeEncodable(String rawText, PDFont font) throws IOException {
            if (rawText == null)
                return "";

            // Normalize a few common problematic punctuation characters.
            String text = rawText
                    .replace("\r", " ")
                    .replace("\n", " ")
                    .replace('\u00A0', ' ')
                    .replace('\u2014', '-') // em dash
                    .replace('\u2013', '-') // en dash
                    .replace("\u2026", "...") // ellipsis
                    .replace('\u00B7', '-'); // middot

            StringBuilder sb = new StringBuilder(text.length());
            for (int i = 0; i < text.length();) {
                int cp = text.codePointAt(i);
                i += Character.charCount(cp);

                if (cp == '\t') {
                    cp = ' ';
                }

                // Drop other ASCII control chars.
                if (cp < 0x20) {
                    continue;
                }

                String ch = new String(Character.toChars(cp));
                try {
                    // getStringWidth will throw IllegalArgumentException if the font can't encode
                    // the character.
                    font.getStringWidth(ch);
                    sb.append(ch);
                } catch (IllegalArgumentException e) {
                    sb.append('?');
                }
            }

            return sb.toString().trim();
        }

        private static List<String> wrapLine(String text, PDFont font, float fontSize, float maxWidth)
                throws IOException {
            if (text == null)
                return List.of("");
            String[] words = text.trim().isEmpty() ? new String[] { "" } : text.split("\\s+");
            java.util.ArrayList<String> lines = new java.util.ArrayList<>();
            StringBuilder current = new StringBuilder();

            for (String w : words) {
                if (current.length() == 0) {
                    current.append(w);
                    continue;
                }

                String candidate = current + " " + w;
                if (textWidth(candidate, font, fontSize) <= maxWidth) {
                    current.append(' ').append(w);
                } else {
                    lines.add(current.toString());
                    current.setLength(0);

                    // If a single word is longer than the line, hard-break it.
                    if (textWidth(w, font, fontSize) > maxWidth) {
                        lines.addAll(hardBreakWord(w, font, fontSize, maxWidth));
                    } else {
                        current.append(w);
                    }
                }
            }

            if (current.length() > 0) {
                lines.add(current.toString());
            }
            return lines;
        }

        private static List<String> hardBreakWord(String word, PDFont font, float fontSize, float maxWidth)
                throws IOException {
            java.util.ArrayList<String> parts = new java.util.ArrayList<>();
            StringBuilder cur = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                char ch = word.charAt(i);
                String candidate = cur.toString() + ch;
                if (!cur.isEmpty() && textWidth(candidate, font, fontSize) > maxWidth) {
                    parts.add(cur.toString());
                    cur.setLength(0);
                }
                cur.append(ch);
            }
            if (!cur.isEmpty())
                parts.add(cur.toString());
            return parts;
        }

        private static float textWidth(String text, PDFont font, float fontSize) throws IOException {
            return font.getStringWidth(text) / 1000f * fontSize;
        }

    }
}
