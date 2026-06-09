package core.db_functions;

// auto-generated field values
// author: Felix D'Cruz

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class CalcField {

    public static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String newUUID() {
        return UUID.randomUUID().toString();
    }

    public static String nowDatetime() {
        return LocalDateTime.now().format(DATETIME_FMT);
    }

    public static String nowDate() {
        return LocalDate.now().format(DATE_FMT);
    }

    // calculates the monthly saving needed to reach 'amount' between two ISO date strings.
    // returns amount unchanged if dates are unparseable or the period is zero/negative.
    public static double calcPerMonth(double amount, String startDate, String endDate) {
        if (startDate == null || endDate == null) return amount;
        try {
            LocalDate start = LocalDate.parse(startDate, DATE_FMT);
            LocalDate end   = LocalDate.parse(endDate,   DATE_FMT);
            long months = ChronoUnit.MONTHS.between(start, end);
            if (months <= 1) return amount;
            return amount / months;
        } catch (Exception e) {
            System.err.println("CalcField.calcPerMonth: could not parse dates '" + startDate + "' / '" + endDate + "'");
            return amount;
        }
    }
}
