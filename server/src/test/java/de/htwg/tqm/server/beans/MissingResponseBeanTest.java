package de.htwg.tqm.server.beans;

import de.htwg.tqm.server.beans.AbstractBeanTest;
import de.htwg.tqm.server.beans.MissingResponseBean;

public class MissingResponseBeanTest extends AbstractBeanTest {

    @Override
    protected Object createSut() {
        return new MissingResponseBean(1, "lazy user");
    }

}