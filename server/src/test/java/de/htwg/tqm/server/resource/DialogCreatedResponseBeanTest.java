package de.htwg.tqm.server.resource;

import de.htwg.tqm.server.beans.AbstractBeanTest;

public class DialogCreatedResponseBeanTest extends AbstractBeanTest {


    @Override
    protected Object createSut() {
        return new DialogCreatedResponseBean(5);
    }
}