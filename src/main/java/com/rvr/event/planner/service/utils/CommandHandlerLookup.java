package com.rvr.event.planner.service.utils;

import com.rvr.event.planner.domain.Command;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class CommandHandlerLookup {
    private Map<Class<? extends Command>, Class<?>> commandHandlers = new HashMap<Class<? extends Command>, Class<?>>();

    @SuppressWarnings("unchecked")
    @Deprecated
    public CommandHandlerLookup(String methodName, Class<?>... aggregateTypes) {
        for (Class<?> type : aggregateTypes) {
            loadMethods(methodName, type);
        }
    }

    @Deprecated
    public Class<?> targetType(Command command) {
        return commandHandlers.get(command.getClass());
    }

    private void loadMethods(String methodName, Class<?> type) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(methodName)
                    && isOneComandParameter(method)) {
                commandHandlers.put((Class<? extends Command>) method.getParameterTypes()[0], type);
            }
        }
    }

    private boolean isOneComandParameter(Method method) {
        return method.getParameterTypes().length == 1 &&
                Command.class.isAssignableFrom(method.getParameterTypes()[0]);
    }


}