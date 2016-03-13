package de.htwg.tqm.server.resource;

import de.htwg.tqm.server.beans.AbstractBeanTest;

public class DialogResolveRequestBeanTest extends AbstractBeanTest {

    @Override
    protected Object createSut() {
        return new DialogResolveRequestBean("user");
    }
}