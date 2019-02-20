public enum MessageType {
    REGISTER("REGISTER"),
    LOOKUP("LOOKUP");

    private final String text;

    /**
     * @param text
     */
    MessageType(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}
