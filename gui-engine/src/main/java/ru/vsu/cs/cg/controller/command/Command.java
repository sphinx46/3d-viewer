package ru.vsu.cs.cg.controller.command;

public interface Command {
    void execute();
    String getName();
    String getDescription();
}
