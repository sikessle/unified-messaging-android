package de.htwg.tqm.server.beans;

import java.util.*;

public class DialogBeanTest extends AbstractBeanTest {

    @Override
    protected Object createSut() {
        DialogMessage message = new DialogMessageBean("author", 5, "body");
        SortedSet<DialogMessage> messages = new TreeSet<>(Collections.singleton(message));
        return new DialogBean(1, "subject", 2, messages, "initiator", "affected", false, false, 10L);
    }
}