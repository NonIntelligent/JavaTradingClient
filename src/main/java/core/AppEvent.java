package core;

import utility.Consumer;

public record AppEvent(Object data, AppEventType type, Consumer sender) { }