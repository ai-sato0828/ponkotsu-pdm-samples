import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlWhitespaceCheck {
    public static void main(String[] args) throws Exception {
        Path broken = Path.of("broken.xml");
        Path fixed = Path.of("fixed.xml");
        String original = Files.readString(broken);
        String repaired = Files.readString(fixed);
        String tag = "<beans:beans";

        if (original.codePointAt(tag.length()) != 0x00A0
                || repaired.codePointAt(tag.length()) != 0x0020
                || !original.replace(tag + "\u00a0", tag + " ").equals(repaired)) {
            throw new AssertionError("expected one change to the tag's attribute separator");
        }

        try {
            parse(broken);
            throw new AssertionError("U+00A0 was accepted as XML syntax whitespace");
        } catch (SAXParseException expected) {
            System.out.printf("broken.xml: U+00A0 rejected (line %d)%n", expected.getLineNumber());
        }
        parse(fixed);
        System.out.println("fixed.xml: U+0020 accepted");
    }

    private static void parse(Path path) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new DefaultHandler() {
            @Override
            public void fatalError(SAXParseException error) throws SAXParseException {
                throw error;
            }
        });
        builder.parse(path.toFile());
    }
}
