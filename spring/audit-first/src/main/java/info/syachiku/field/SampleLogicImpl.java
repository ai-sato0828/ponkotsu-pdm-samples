package info.syachiku.field;

import org.springframework.stereotype.Component;

@Component("sampleLogicImpl")
public class SampleLogicImpl implements SampleLogic {
    @Override
    public String run() {
        return "A";
    }
}
