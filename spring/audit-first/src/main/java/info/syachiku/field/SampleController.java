package info.syachiku.field;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class SampleController {

    @Autowired
    @Qualifier("sampleLogicImpl2")
    private SampleLogic sampleLogic;

    public String execute() {
        return sampleLogic.run();
    }
}
