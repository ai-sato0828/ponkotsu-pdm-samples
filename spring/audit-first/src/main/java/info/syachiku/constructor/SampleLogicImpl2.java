package info.syachiku.constructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SampleLogicImpl2 extends AbstractLogic implements SampleLogic {

    public SampleLogicImpl2(
            @Qualifier("sampleUtilImpl") SampleUtil sampleUtil,
            SampleClass sampleClass) {
        super(sampleUtil, sampleClass);
    }

    @Override
    public String run() {
        return common();
    }
}
