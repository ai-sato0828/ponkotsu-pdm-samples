package info.syachiku.field;

import org.springframework.stereotype.Component;

@Component("sampleLogicImpl2")
public class SampleLogicImpl2 implements SampleLogic {
    @Override
    public String run() {
        return "B";
    }
}
