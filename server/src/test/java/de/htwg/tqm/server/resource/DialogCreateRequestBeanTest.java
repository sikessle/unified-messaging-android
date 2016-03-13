package de.htwg.tqm.server.resource;

import de.htwg.tqm.server.beans.AbstractBeanTest;

public class DialogCreateRequestBeanTest extends AbstractBeanTest {


    @Override
    protected Object createSut() {
        return new DialogCreateRequestBean("subject", 5, "initiator", "affected");
    }
}