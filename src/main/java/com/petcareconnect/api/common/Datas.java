package com.petcareconnect.api.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public final class Datas {

    private static final DateTimeFormatter ESTRITA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Datas() {
    }

    // Aceita somente datas reais no formato dd/MM/yyyy
    public static LocalDate ler(String texto) {
        try {
            return LocalDate.parse(texto.trim(), ESTRITA);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new NegocioException("Data inválida. Use o formato dd/MM/yyyy.");
        }
    }
}