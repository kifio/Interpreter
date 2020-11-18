package controller;

import controller.workers.HighlighterWorker;

import java.util.Set;

public interface OnCodeHighlightedListener {
    void onCodeHighlighted(Set<HighlighterWorker.Attributes> attributes, int offset, int length);
}
