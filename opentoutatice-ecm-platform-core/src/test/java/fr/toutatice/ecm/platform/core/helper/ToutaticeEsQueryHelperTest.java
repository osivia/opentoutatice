package fr.toutatice.ecm.platform.core.helper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.IterableQueryResult;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

import fr.toutatice.ecm.platform.core.query.helper.ToutaticeEsQueryHelper;
import junit.framework.Assert;

@RunWith(FeaturesRunner.class)
@Features({CoreFeature.class, RepositoryElasticSearchFeature.class})
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.CLASS)
public class ToutaticeEsQueryHelperTest {

    @Inject
    CoreSession session;

    @Test
    public void testUnrestrictedQueryAndAggregate() {
        String query = String.format(
                "select ttc:webid from Document where ecm:mixinType = 'Folderish' "
                        + "and ecm:ancestorId = '%s' and ecm:currentLifeCycleState = 'deleted' and ecm:isVersion = 0 and ecm:isProxy = 0",
                session.getRootDocument().getId());

        IterableQueryResult itRes = ToutaticeEsQueryHelper.unrestrictedQueryAndAggregate(session, query);
        if (itRes != null) {
            Assert.assertTrue(itRes.size() > 0);
        }
    }

}
