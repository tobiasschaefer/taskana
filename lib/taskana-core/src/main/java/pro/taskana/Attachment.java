package pro.taskana;

import java.sql.Timestamp;
import java.util.Map;

import pro.taskana.model.ObjectReference;

/**
 * Attachment-Interface to specify Attachment Attributes.
 */
public interface Attachment {

    /**
     * Returns the current id of the attachment.
     *
     * @return ID of attachment
     */
    String getId();

    /**
     * Returns the id of the associated task.
     *
     * @return taskId
     */
    String getTaskId();

    /**
     * Returns the time when the attachment was created.
     *
     * @return the created time as {@link Timestamp}
     */
    Timestamp getCreated();

    /**
     * Returns the time when the attachment was last modified.
     *
     * @return modified {@link Timestamp} of the attachment
     */
    Timestamp getModified();

    /**
     * Returns the classification of the attachment.
     *
     * @return the {@link Classification} of this attachment
     */
    Classification getClassification();

    /**
     * Set the classification for this attachment.
     *
     * @param classification
     *            the {@link Classification} for this attachment
     */
    void setClassification(Classification classification);

    /**
     * Returns the {@link ObjectReference primaryObjectReference} of the attachment.
     *
     * @return {@link ObjectReference primaryObjectReference} of the attachment
     **/
    ObjectReference getObjectReference();

    /**
     * Sets the {@link ObjectReference primaryObjectReference} of the attachment.
     *
     * @param objectReference
     *            the {@link ObjectReference primaryObjectReference} of the attachment
     */
    void setObjectReference(ObjectReference objectReference);

    /**
     * Returns the Channel on which the attachment was received.
     *
     * @return the Channel on which the attachment was received.
     **/
    String getChannel();

    /**
     * Sets the Channel on which the attachment was received.
     *
     * @param channel
     *            the channel on which the attachment was received
     */
    void setChannel(String channel);

    /**
     * Returns the time when this attachment was received.
     *
     * @return received time as exact {@link Timestamp}
     */
    Timestamp getReceived();

    /**
     * Sets the time when the attachment was received.
     *
     * @param received
     *            the time as {@link Timestamp} when the attachment was received
     **/
    void setReceived(Timestamp received);

    /**
     * Returns the custom attributes of this attachment.
     *
     * @return customAttributes as {@link Map}
     */
    Map<String, Object> getCustomAttributes();

    /**
     * Sets the custom attribute Map of the attachment.
     *
     * @param customAttributes
     *            a {@link Map} that contains the custom attributes of the attachment as key, value pairs
     */
    void setCustomAttributes(Map<String, Object> customAttributes);

}
