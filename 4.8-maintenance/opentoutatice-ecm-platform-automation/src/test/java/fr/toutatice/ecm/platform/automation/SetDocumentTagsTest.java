package fr.toutatice.ecm.platform.automation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.tag.Tag;
import org.nuxeo.ecm.platform.tag.TagService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class SetDocumentTagsTest {

	@Tested
	private SetDocumentTags setDocumentTags;

	// @Mocked
	@Injectable
	private TagService tagServiceMock;

	@Injectable
	private CoreSession sessionMock;

	private DocumentModel buildFakeDocumentModel(final String docId) {

		return new MockUp<DocumentModel>() {

			@Mock
			public String getId() {
				return docId;
			}
		}.getMockInstance();
	}

	@Test
	public void testSetTagsForNewDoc() throws Exception {
		// INIT
		final String username = "testUser";
		final String[] labels = { "label1", "label2" };
		final String docId = "docId";
		final DocumentModel doc = buildFakeDocumentModel(docId);

		setDocumentTags.username = username;
		setDocumentTags.labels = new StringList(labels);

		new Expectations() {
			{
				tagServiceMock.getDocumentTags(sessionMock, docId, username);
				result = null;

			}
		};

		// TEST
		setDocumentTags.run(doc);

		// VERIFY
		new Verifications() {
			{
				tagServiceMock.tag(sessionMock, docId, labels[0], username);
				tagServiceMock.tag(sessionMock, docId, labels[1], username);

				sessionMock.save();

			}
		};

	}

	@Test
	public void testSetEmptyTags() throws Exception {
		// INIT
		final String username = "testUser";
		final String[] labels = {};
		final String docId = "docId";
		final DocumentModel doc = buildFakeDocumentModel(docId);

		setDocumentTags.username = username;
		setDocumentTags.labels = new StringList(labels);

		new Expectations() {
			{
				tagServiceMock.getDocumentTags(sessionMock, docId, username);
				result = null;

			}
		};

		// TEST
		setDocumentTags.run(doc);

		// VERIFY
		new Verifications() {
			{
				// tag must never be called
				tagServiceMock.tag((CoreSession) any, anyString, anyString, anyString);
				times = 0;

				sessionMock.save();

			}
		};
	}

	@Test
	public void testSetNullTags() throws Exception {
		// INIT
		final String username = "testUser";
		final String docId = "docId";
		final DocumentModel doc = buildFakeDocumentModel(docId);

		setDocumentTags.username = username;
		setDocumentTags.labels = null;

		new Expectations() {
			{
				tagServiceMock.getDocumentTags(sessionMock, docId, username);
				result = null;

			}
		};

		// TEST
		setDocumentTags.run(doc);

		// VERIFY
		new Verifications() {
			{
				// tag must never be called
				tagServiceMock.tag((CoreSession) any, anyString, anyString, anyString);
				times = 0;

				sessionMock.save();
			}
		};
	}

	@Test
	public void testSetInvalidTags() throws Exception {
		// INIT
		final String username = "testUser";
		// tag number 1, 3 and 4 are invalid and should be ignored
		// tag number 0, 3 and 5 are valid
		final String[] labels = { "label1", "", "label2", " ", null, "label3" };
		final String docId = "docId";
		final DocumentModel doc = buildFakeDocumentModel(docId);

		setDocumentTags.username = username;
		setDocumentTags.labels = new StringList(labels);

		new Expectations() {
			{
				tagServiceMock.getDocumentTags(sessionMock, docId, username);
				result = null;

			}
		};

		// TEST
		setDocumentTags.run(doc);

		// VERIFY
		new Verifications() {
			{
				tagServiceMock.tag(sessionMock, docId, labels[0], username);// "label1" -> valid
				times = 1;
				tagServiceMock.tag(sessionMock, docId, labels[1], username);// "" -> invalid (empty)
				times = 0;
				tagServiceMock.tag(sessionMock, docId, labels[2], username);// "label2" -> valid
				times = 1;
				tagServiceMock.tag(sessionMock, docId, labels[3], username);// " " -> invalid (blank)
				times = 0;
				tagServiceMock.tag(sessionMock, docId, labels[4], username);// null -> invalid
				times = 0;
				tagServiceMock.tag(sessionMock, docId, labels[5], username);// "label3" -> valid
				times = 1;

				sessionMock.save();

			}
		};

	}

	@Test
	public void testReplaceTags() throws Exception {
		// INIT
		final String username = "testUser";
		final String[] labels = { "new_tag1", "new_tag2" };
		final String docId = "docId";
		final DocumentModel doc = buildFakeDocumentModel(docId);

		final List<Tag> existingTags = Collections.unmodifiableList(Arrays.asList(new Tag("existing_tag1", 0), new Tag("existing_tag2", 0), new Tag("existing_tag3", 0)));

		setDocumentTags.username = username;
		setDocumentTags.labels = new StringList(labels);

		new Expectations() {
			{
				tagServiceMock.getDocumentTags(sessionMock, docId, username);
				result = existingTags;

			}
		};

		// TEST
		setDocumentTags.run(doc);

		// VERIFY
		new Verifications() {
			{
				// untag should be called for every existing tags
				tagServiceMock.untag(sessionMock, docId, existingTags.get(0).getLabel(), username);
				tagServiceMock.untag(sessionMock, docId, existingTags.get(1).getLabel(), username);
				tagServiceMock.untag(sessionMock, docId, existingTags.get(2).getLabel(), username);

				// new tags should be set
				tagServiceMock.tag(sessionMock, docId, labels[0], username);
				tagServiceMock.tag(sessionMock, docId, labels[1], username);

				sessionMock.save();
			}
		};
	}

	@Test
	public void testReplaceTagsForIdRef() throws Exception {
		// INIT
		final String username = "testUser";
		final String[] labels = { "new_tag1", "new_tag2" };
		final String docId = "docId";
		final IdRef idRef = new IdRef(docId);

		final List<Tag> existingTags = Collections.unmodifiableList(Arrays.asList(new Tag("existing_tag1", 0), new Tag("existing_tag2", 0), new Tag("existing_tag3", 0)));

		setDocumentTags.username = username;
		setDocumentTags.labels = new StringList(labels);

		new Expectations() {
			{
				tagServiceMock.getDocumentTags(sessionMock, docId, username);
				result = existingTags;

			}
		};

		// TEST
		setDocumentTags.run(idRef);

		// VERIFY
		new Verifications() {
			{
				// untag should be called for every existing tags
				tagServiceMock.untag(sessionMock, docId, existingTags.get(0).getLabel(), username);
				tagServiceMock.untag(sessionMock, docId, existingTags.get(1).getLabel(), username);
				tagServiceMock.untag(sessionMock, docId, existingTags.get(2).getLabel(), username);

				// new tags should be set
				tagServiceMock.tag(sessionMock, docId, labels[0], username);
				tagServiceMock.tag(sessionMock, docId, labels[1], username);

				sessionMock.save();
			}
		};
	}

	@Test
	public void testRmoveAllTags() throws Exception {
		// INIT
		final String username = "testUser";
		final String[] labels = {};
		final String docId = "docId";
		final DocumentModel doc = buildFakeDocumentModel(docId);

		final List<Tag> existingTags = Collections.unmodifiableList(Arrays.asList(new Tag("existing_tag1", 0), new Tag("existing_tag2", 0), new Tag("existing_tag3", 0)));

		setDocumentTags.username = username;
		setDocumentTags.labels = new StringList(labels);

		new Expectations() {
			{
				tagServiceMock.getDocumentTags(sessionMock, docId, username);
				result = existingTags;

			}
		};

		// TEST
		setDocumentTags.run(doc);

		// VERIFY
		new Verifications() {
			{
				// untag should be called for every existing tags
				tagServiceMock.untag(sessionMock, docId, existingTags.get(0).getLabel(), username);
				tagServiceMock.untag(sessionMock, docId, existingTags.get(1).getLabel(), username);
				tagServiceMock.untag(sessionMock, docId, existingTags.get(2).getLabel(), username);

				// tag must never be called
				tagServiceMock.tag((CoreSession) any, anyString, anyString, anyString);
				times = 0;

				sessionMock.save();
			}
		};
	}

}
