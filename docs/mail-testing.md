# Mail Testing

## Mail Server Configuration
### Mail Server JSON Profiles
Mail servers configuration is done in a profile json file the same as application and WebDriver configuration.
```json
{
    "dev": {
        "domain": "mail.example.com",
        "host": "mail.example.com",
        "username": "username",
        "password": "password",
        "popPort": -1,
        "smtpPort": 25,
        "useTls": false,
        "allowInsecure": true
    },
    "test-pop": {
        "domain": "test1.mail.example.com",
        "host": "test1.mail.example.com",
        "username": "username",
        "password": "password",
        "popPort": -1,
        "useTls": true,
        "allowInsecure": false
    },
    "test-smtp": {
        "domain": "test2.mail.example.com",
        "host": "test2.mail.example.com",
        "username": "username",
        "password": "password",
        "smtpPort": 25,
        "useTls": true,
        "allowInsecure": false
    }
}
```
Here we are defining 3 mail server profiles, `dev`, `test-pop`, and `test-smtp`.

POP and SMTP servers may be defined together if they share the same details, or they may be defined separately.  The POP server receives mail, and the SMTP server sends mail. 

When `popPort` is set to `-1` it will use the default port for either secure or insecure.

### MailAuthenticator, PopServer, SmtpServer
The json profiles are loaded into MailAuthenticators, which are used to create `PopServer` and `SmtpServer` instances.

```java
ProfileMap<MailAuthenticator> mailProfiles = ProfileMap.loadFile(MailAuthenticator.class, "../src/test/resources/profiles.mail.json");
MailAuthenticator popMailAuth = mailProfiles.getProfile(readSystemProperty("mail.pop", "dev"));
MailAuthenticator smtpMailAuth = mailProfiles.getProfile(readSystemProperty("mail.smtp", "dev"));
PopServer popServer = new PopServer(popMailAuth);
SmtpServer smtpServer = new SmtpServer(smtpMailAuth);
```
This will read the json mail file and load MailAuthenticators for a PopServer and SmtpServer.

The PopServer and SmtpServer objects are not used directly for fetching and sending mail.  They will be passed in to a `MailTester` to be used.

`readSystemProperty` is used to define a default profile that can be overwritten in the `user.defaults` file by adding `mail.pop=test-pop` or with a system variable.

## Accessing the MailTester
There are two ways to access a `MailTester` in your tests, either standalone or from within a `PageModel` chain.

### Standalone:
```java
Mail.testMail(context)
        .fetchMail(...)
```
### From PageModel
```java
context.getLoginPage()
        .testMail()
        .fetchMail(...)
```
## Fetching Mail
The `MailTester` has different `fetchMail` methods for finding and fetching specific mail messages from a `PopServer`.
After fetching a mail message you can perform tests on the message or store the `MailMessage` to your `TestContext` for later use.  

There are two simple `fetchMail` methods for finding by subject 
```java
```
and recipient, and an advanced method with a `MailTester` lambda.
```java
```

### Simple mail fetching
Fetch mail with an exact subject, and fetch mail with an exact subject and recipient:

```java

```
These methods will find the most recent matching mail on the server if it exists.  If the message is not found, it will retry for up to 30 seconds before failing the test.
(Note: if not using a unique subject string, make sure you have deleted matching mail from previous test runs)

After fetching the message you can perform tests or store it:
#### Test
```java
```
#### Store
```java
```

### Advanced mail fetching - Mail Predicates:
The advanced `fetchMail` takes a `mailPredicate` which is a lambda for a `MailMessageTester`.  You can use this `MailMessageTester` to test any part of a `MailMessage`.

In these examples we will use the `mailPredicate` to do the same simple fetch in the previous example:
```java
```

Here we will look for mail from a specific sender with a phrase in the body and containing 3 attachments:
```java
```

Note: the `mailPredicate` will potentially be tested against all mail in the mailbox.  This should be the minimum needed to identify a mail message, and after fetching you can perform tests on the mail message.  Misusing this can result in your test waiting for the `timeout` before failing.

## Testing Mail
The `MailMessageTester` allows testing any property of a MailMessage.
```
testSender
testRecipientsTo
testRecipientsCc
testRecipientsBcc
testRecipientsAll
testSubject
testTextBody
testHtmlBody
testHeader
testHeaderList
testSentDate
testAttachment
testAttachmentCount
```
The `MailMessageTester` also contains a `doAction` method similar to `PageModel` for any other complex testing you may need to do.

### Testing Mail Fields

#### Storing Values from Mail

#### Opening Mail Links

#### Testing HTML body with PageModel

### Testing Attachments
#### Text Attachments

#### Attachment Byte Stream

#### Save attachment as file

### Testing Mail not Found

## Sending Mail
### Composing a MailMessage
#### Required fields:
When composing an email, the `sender` and `recipient` are the only required fields that must be set.  A unique subject and body will be generated if not set.

#### Unique Tagging:
By default a unique string will be appended when using `.subjet()`, `.body()`, `.textBody()`, and `.htmlBody()`.

To set a subject or body without appending a unique value, use `.subjectUntagged()`, `.bodyUntagged()`, `.textBodyUntagged()`, or `.htmlBodyUntagged()`

#### Mail Body:
Emails may contain different HTML and Plaintext body parts.  The `.body()` method will set both the html and text body parts to the same value.  To set different html and text parts use `.textBody()` and `.htmlBody()`

### Adding attachments:
Attachments contain 3 parts: `filename`, `content bytes`, and `content type`

The `.withAttachment()` method with no parameters will generate a plaintext attachment with a random filename and contents.

The `withAttachment(String filename, String contents)` will create an attachment with content type `text/plain`

The `.withAttachment(File file, String contentType)` and `.withAttachment(String filename, File file, String contentType)` method will load the byte contents from the File and create an attachment with the file's filename or the given filename.

Use the `withAttachment(String filename, byte[] contents, String contentType)` method for creating an attachment from a byte array.

### Sending and Storing Composed Mail
After composing a `MailMessage` you can send the mail with an SMTP server.
```java
```
Since the composed mail often contains unique and unknown strings for the subject and body, it is useful to store the `MailMessage` to access those values.  This is done with the `sendAndStore` method.
```java
```

### Fetching Sent Mail
After sending a MailMessage there are convenience methods to fetch the sent mail from the receiving mailbox.

`fetchSentBySubject` - will fetch the received email with the same subject.

`fetchSentBySubjectAndRecipient` - will fetch the received email with the same subject and the given recipient.

`fetchSent` - takes a lambda which is passed the sent `MailMessage` and `MailMessageTester` for matching mail to fetch.  This gives you access to any unique values generated in the email without needing to store it.