package com.wisegrade.exam.model;

public enum RespuestaCorrecta {
    A, B, C, D;

    public char asChar() {
        return name().charAt(0);
    }
}
