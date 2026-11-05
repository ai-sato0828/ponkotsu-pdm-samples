package info.syachiku.stacktrace.source;

import org.springframework.core.convert.converter.Converter;

public class FixedQuantityConverter implements Converter<String, Quantity> {
    @Override
    public Quantity convert(String source) {
        return new Quantity(Integer.parseInt(source.strip()));
    }
}
