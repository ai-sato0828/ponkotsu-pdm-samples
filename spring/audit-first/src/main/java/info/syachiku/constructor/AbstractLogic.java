package info.syachiku.constructor;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractLogic {
    private final SampleUtil sampleUtil;
    private final SampleClass sampleClass;

    protected String common() {
        return sampleClass.prefix() + sampleUtil.message();
    }
}
