package top.clspd.shellauthorization.Shared.ShellProvider.data;

public class ShellProviderParameter {
    public enum Type {
        STRING,
        INT,
        BOOLEAN,
    }

    private final String key;
    private final String label;
    private final Type type;
    private final boolean required;
    private final Object defaultValue;
    private Object value;

    public ShellProviderParameter(String key, String label, Type type, boolean required, Object defaultValue) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    public String getKey() { return key; }
    public String getLabel() { return label; }
    public Type getType() { return type; }
    public boolean isRequired() { return required; }
    public Object getDefaultValue() { return defaultValue; }

    public Object getValue() {
        return value != null ? value : defaultValue;
    }

    public Object getRawValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public ShellProviderParameter withValue(Object v) {
        ShellProviderParameter p = new ShellProviderParameter(key, label, type, required, defaultValue);
        p.value = v;
        return p;
    }
}
