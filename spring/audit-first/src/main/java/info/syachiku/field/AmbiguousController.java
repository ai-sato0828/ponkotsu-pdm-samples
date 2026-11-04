package info.syachiku.field;

import org.springframework.beans.factory.annotation.Autowired;

public class AmbiguousController {

    @Autowired
    private SampleLogic sampleLogic;

    public String execute() {
        return sampleLogic.run();
    }
}
