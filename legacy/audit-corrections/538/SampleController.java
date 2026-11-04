public final class SampleController {
    public static void main(String[] args) throws Exception {
        try (var api = new LocalUserApi(true)) {
            var client = new UserRequestService(api.baseUrl());
            check(client.findUsers(), 200, "0");
            checkCounts(api, 2, 0);
            System.out.println("GET retried: requests=2, saved=0");
        }

        try (var api = new LocalUserApi(true)) {
            var client = new UserRequestService(api.baseUrl());
            check(client.registerWithoutKeyAndRetryForComparison("Aki"), 201, "2");
            checkCounts(api, 2, 2);
            System.out.println("POST without key, blindly retried: requests=2, saved=2");
        }

        try (var api = new LocalUserApi(true)) {
            var client = new UserRequestService(api.baseUrl());
            check(client.registerUser("Aki", "registration-7"), 201, "1");
            checkCounts(api, 2, 1);
            System.out.println("POST with same key and body: requests=2, saved=1, id=1");
        }

        try (var api = new LocalUserApi(false)) {
            var client = new UserRequestService(api.baseUrl());
            check(client.registerUser("Aki", "registration-7"), 201, "1");
            check(client.registerUser("Yui", "registration-7"), 409,
                    "same key with different name");
            checkCounts(api, 2, 1);
            System.out.println("POST with same key and changed body: HTTP 409, saved=1");
        }

        try (var api = new LocalUserApi(true)) {
            var client = new UserRequestService(api.baseUrl());
            try {
                client.registerUser("Aki", null);
                throw new AssertionError("expected unknown result after response timeout");
            } catch (AbstractApi.ResponseTimeout expected) {
                checkCounts(api, 1, 1);
                System.out.println("POST without key, stopped: requests=1, saved=1");
            }
        }
    }

    private static void check(AbstractApi.Response response, int status, String body) {
        if (response.status() != status || !response.body().equals(body)) {
            throw new AssertionError("unexpected response: " + response);
        }
    }

    private static void checkCounts(LocalUserApi api, int requests, int saved) {
        if (api.requests() != requests || api.savedUsers() != saved) {
            throw new AssertionError("requests=" + api.requests() + ", saved=" + api.savedUsers());
        }
    }
}
