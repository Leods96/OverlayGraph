package user_interface;

public enum MenuState {
    DISTANCE_COMPUTATION,
    CONFIGURATION,
    OVERLAY_MANAGEMENT,
    HELP;

    static public MenuState getStateFromNum(int choice) {
        return MenuState.values()[choice - 1];
    }
}
