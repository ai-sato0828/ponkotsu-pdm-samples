package info.syachiku.constructor;

import org.springframework.stereotype.Component;

@Component
public class AmbiguousLogic extends AbstractLogic implements SampleLogic {

    public AmbiguousLogic(SampleUtil sampleUtil, SampleClass sampleClass) {
        super(sampleUtil, sampleClass);
    }

    @Override
    public String run() {
        return common();
    }
}
