package eu.freme.broker.security.api.samplestuff;

import eu.freme.broker.security.domain.DomainUser;
import eu.freme.broker.security.domain.Stuff;

import java.util.List;

public interface ServiceGateway {
    List<Stuff> getSomeStuff();

    void createStuff(Stuff newStuff, DomainUser domainUser);
}
