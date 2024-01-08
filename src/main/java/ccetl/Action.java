package ccetl;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public enum Action {
    FILE {
        @Override
        void run(String message) {
            Sjl.write(message);
        }
    },
    PRINT {
        @Override
        void run(String message) {
            System.out.println(message);
        }
    },
    PRINT_AND_FILE {
        @Override
        void run(String message) {
            Sjl.write(message);
            System.out.println(message);
        }
    },
    CUSTOM {
        @Override
        void run(String message) {
            Action.custom.accept(message);
        }
    };

    private static Consumer<String> custom = s -> {
        throw new RuntimeException(new IllegalStateException());
    };

    abstract void run(String message);

    public static void setCustom(Consumer<String> custom) {
        Action.custom = custom;
    }
}
