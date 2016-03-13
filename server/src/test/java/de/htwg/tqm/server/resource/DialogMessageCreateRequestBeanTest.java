package de.htwg.tqm.server.resource;

import de.htwg.tqm.server.beans.AbstractBeanTest;
import de.htwg.tqm.server.beans.Client;

public class DialogMessageCreateRequestBeanTest extends AbstractBeanTest {


    @Override
    protected Object createSut() {
        return new DialogMessageCreateRequestBean("user", "body");
    }
}