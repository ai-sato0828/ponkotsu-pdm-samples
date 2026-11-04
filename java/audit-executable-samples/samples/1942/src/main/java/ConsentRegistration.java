final class ConsentRegistration {
    enum Outcome { ASK_AGAIN, REGISTER_ONLY, REGISTER_AND_SUBSCRIBE }

    Outcome register(Boolean agreed, Runnable saveUser, Runnable subscribe) {
        if (agreed == null) {
            return Outcome.ASK_AGAIN;
        }
        saveUser.run();
        if (Boolean.TRUE.equals(agreed)) {
            subscribe.run();
            return Outcome.REGISTER_AND_SUBSCRIBE;
        }
        return Outcome.REGISTER_ONLY;
    }

    boolean shouldExecute(Boolean enabled) {
        return Boolean.TRUE.equals(enabled);
    }
}
