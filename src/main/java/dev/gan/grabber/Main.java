package dev.gan.grabber;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.lang.String.format;

/**
 * Main class for running the grabber
 *
 * @author gan
 */
public class Main {

    private static final String URL = "https://hh.ru/search/vacancy?area=113&search_field=name&search_field=company_name&work_format=REMOTE&text=Java+developer";
    private static final int TIMEOUT = 10000;

    private static final Grabber GRABBER = new Grabber();

    public static void main(String[] args) {
        System.out.println("=== HH Grabber Started ===");
        System.out.println("Time: " + getCurrentTime());

        try {
            final List<Vacancy> vacancies = GRABBER.grabVacancies(URL, TIMEOUT);
            System.out.println(format("%s active vacancies grabbed from HH", vacancies.size()));
        } catch (Exception e) {
            System.err.println("Error occurred during parsing: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("=== HH Grabber Completed ===");
    }

    private static String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }
}
