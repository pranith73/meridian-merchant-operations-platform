package com.pranith73.meridian.shared.application;

/**
 * Generic contract for handling application-layer commands.
 *
 * <p>C is the command type.</p>
 * <p>R is the result type returned after the command is handled.</p>
 *
 * <p>This gives later modules a consistent way to model use cases.</p>
 */
public interface CommandHandler<C extends Command<R>, R> {

    /**
     * Handles the incoming command and returns a result.
     *
     * @param command the command object containing the requested action
     * @return the result produced after handling the command
     */
    R handle(C command);
}