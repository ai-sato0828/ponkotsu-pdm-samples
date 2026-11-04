public class JaxbProbe {
    public static void main(String[] args) {
        inspect("javax.xml.bind.JAXBContext");
        inspect("jakarta.xml.bind.JAXBContext");
    }

    private static void inspect(String name) {
        try {
            Class<?> type = Class.forName(name, false, JaxbProbe.class.getClassLoader());
            System.out.println(name + " -> " + type.getProtectionDomain()
                    .getCodeSource().getLocation());
            Object context = type.getMethod("newInstance", Class[].class)
                    .invoke(null, (Object) new Class<?>[]{String.class});
            System.out.println("provider -> " + context.getClass().getName());
        } catch (ClassNotFoundException e) {
            System.out.println(name + " -> missing");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("API found, but context could not be created", e);
        }
    }
}
