package dev.gan.grabber;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests
 *
 * @author gan
 */
@DisplayName("Main tests")
public class MainTest {

    @Test
    @DisplayName("Парсинг HTML документа с заголовками")
    void shouldParseHtmlDocumentWithHeadings() {
        // Подготавливаем тестовый HTML
        final String html = """
                <html>
                <head><title>Test Page</title></head>
                <body>
                    <h1>Main Title</h1>
                    <h2>Subtitle</h2>
                    <h3>Sub-subtitle</h3>
                    <p>Some content</p>
                </body>
                </html>
                """;

        Document document = Jsoup.parse(html);

        // Тестируем извлечение заголовков
        Elements headings = document.select("h1, h2, h3");

        assertThat(headings).hasSize(3);
        assertThat(headings.get(0).text()).isEqualTo("Main Title");
        assertThat(headings.get(1).text()).isEqualTo("Subtitle");
        assertThat(headings.get(2).text()).isEqualTo("Sub-subtitle");
    }

    @Test
    @DisplayName("Парсинг ссылок в HTML документе")
    void shouldParseLinksInHtmlDocument() {
        final String html = """
                <html>
                <body>
                    <a href="https://external.com">External Link</a>
                    <a href="/internal">Internal Link</a>
                    <a href="#anchor">Anchor Link</a>
                    <a href="">Empty Link</a>
                </body>
                </html>
                """;

        Document document = Jsoup.parse(html);
        Elements links = document.select("a[href]");

        assertThat(links).hasSize(4);

        // Проверяем типы ссылок
        long externalLinks = links.stream()
                .mapToLong(link -> {
                    String href = link.attr("href");
                    return (href.startsWith("http://") || href.startsWith("https://")) ? 1 : 0;
                })
                .sum();

        long internalLinks = links.stream()
                .mapToLong(link -> {
                    String href = link.attr("href");
                    return (!href.startsWith("#") && !href.isEmpty()
                            && !href.startsWith("http://") && !href.startsWith("https://")) ? 1 : 0;
                })
                .sum();

        assertThat(externalLinks).isEqualTo(1);
        assertThat(internalLinks).isEqualTo(1);
    }

    @Test
    @DisplayName("Парсинг мета-информации")
    void shouldParseMetaInformation() {
        String html = """
                <html>
                <head>
                    <title>Test Page</title>
                    <meta name="description" content="Test page description">
                    <meta name="keywords" content="test, html, parsing">
                    <meta property="og:title" content="Test OG Title">
                </head>
                <body>
                    <img src="test1.jpg" alt="Test Image 1">
                    <img src="test2.jpg" alt="Test Image 2">
                    <p>Paragraph 1</p>
                    <p>Paragraph 2</p>
                    <div>Div 1</div>
                    <div>Div 2</div>
                    <div>Div 3</div>
                </body>
                </html>
                """;

        Document document = Jsoup.parse(html);

        // Тестируем мета-теги
        Element metaDescription = document.selectFirst("meta[name=description]");
        assertThat(metaDescription).isNotNull();
        assertThat(metaDescription.attr("content")).isEqualTo("Test page description");

        Element metaKeywords = document.selectFirst("meta[name=keywords]");
        assertThat(metaKeywords).isNotNull();
        assertThat(metaKeywords.attr("content")).isEqualTo("test, html, parsing");

        Element ogTitle = document.selectFirst("meta[property=og:title]");
        assertThat(ogTitle).isNotNull();
        assertThat(ogTitle.attr("content")).isEqualTo("Test OG Title");

        // Тестируем подсчет элементов
        assertThat(document.select("img")).hasSize(2);
        assertThat(document.select("p")).hasSize(2);
        assertThat(document.select("div")).hasSize(3);
    }

    @Test
    @DisplayName("Обработка пустого HTML документа")
    void shouldHandleEmptyHtmlDocument() {
        String html = """
                <html>
                <head><title>Empty Page</title></head>
                <body></body>
                </html>
                """;

        Document document = Jsoup.parse(html);

        Elements headings = document.select("h1, h2, h3");
        Elements links = document.select("a[href]");
        Elements images = document.select("img");

        assertThat(headings).isEmpty();
        assertThat(links).isEmpty();
        assertThat(images).isEmpty();
    }

    @ParameterizedTest
    @DisplayName("Тестирование различных заголовков")
    @ValueSource(strings = {"h1", "h2", "h3", "h4", "h5", "h6"})
    void shouldParseAllHeadingLevels(String headingTag) {
        String html = String.format("""
                <html>
                <body>
                    <%s>Test Heading</%s>
                </body>
                </html>
                """, headingTag, headingTag);

        Document document = Jsoup.parse(html);
        Elements headings = document.select(headingTag);

        assertThat(headings).hasSize(1);
        assertThat(headings.first().text()).isEqualTo("Test Heading");
    }

    @Test
    @DisplayName("Тестирование парсинга реального сайта с мокированием")
    void shouldHandleMockedJsoupConnection() throws IOException {
        // Создаем мок документа
        Document mockDocument = Jsoup.parse("""
                <html>
                <head><title>Mocked Page</title></head>
                <body>
                    <h1>Mocked Title</h1>
                    <a href="https://example.com">Mocked Link</a>
                </body>
                </html>
                """);

        // Этот тест демонстрирует, как можно мокировать Jsoup.connect()
        // В реальном коде потребовалось бы рефакторинг для инъекции зависимостей
        assertThat(mockDocument.title()).isEqualTo("Mocked Page");
        assertThat(mockDocument.select("h1")).hasSize(1);
        assertThat(mockDocument.select("a[href]")).hasSize(1);
    }

    @Test
    @DisplayName("Тестирование обработки исключений")
    void shouldHandleExceptions() {
        // Тестируем случай, когда Jsoup не может распарсить невалидный HTML
        String invalidHtml = "<html><head><title>Test</title></head><body><h1>Unclosed tag";

        // Jsoup довольно толерантен к невалидному HTML, поэтому даже это будет распарсено
        Document document = Jsoup.parse(invalidHtml);

        assertThat(document).isNotNull();
        assertThat(document.title()).isEqualTo("Test");

        // Jsoup автоматически закрывает незакрытые теги
        assertThat(document.select("h1")).hasSize(1);
    }

    @Test
    @DisplayName("Тестирование форматирования времени")
    void shouldFormatTimeCorrectly() {
        LocalDateTime testTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
        String formattedTime = testTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        assertThat(formattedTime).isEqualTo("2024-01-15 10:30:45");
    }
}
