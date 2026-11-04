package info.syachiku.order;

import java.time.LocalDate;

public record Order(int id, String name, String status, LocalDate date, String staffId) {}
