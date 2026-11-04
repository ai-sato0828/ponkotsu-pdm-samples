package example.mapofquiz;

record UserForm(String department, String extension) {
    String getDepartment() {
        return department;
    }

    String getExtension() {
        return extension;
    }
}
