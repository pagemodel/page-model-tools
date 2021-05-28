# Advanced usage examples

## Open expanding menus automatically when clicking links
```
FoldersLink id "folderLink" FoldersPage
// submenu links will automatically open folders menu
InboxLink @@@SubmenuLink id "inboxFolderLink" InboxFolderPage
SentLink @@@SubmenuLink id "sentFolderLink" SentFolderPage
TrashLink @@@SubmenuLink id "trashFolderLink" TrashFolderPage
```
`SubmenuLink.pagemodel`
```java
ComponentModel com.example.pmt.pages

# Find element to open the menu
* SectionControl @@@SubmenuLink xpath ".//ancestor::div[contains(@class,'znav-items')]/preceding-sibling::div[2]/a"

%%start
    //Override the element tester click() to open the menu before clicking
    @Override
    public P click() {
        openLinkSection();
        return super.click();
    }

    protected void openLinkSection(){
        LocatedWebElement el = callRef();
        // Check if this link is already visible
        if(!el.hasElement() || el.isDisplayed()){
            return;
        }
        //if not visible, click the menu open and wait for menu to open
        if(getSectionControl().hasElement()){
            //section control is also a SubmenuLink and will attempt to open their section if needed, opening nested submenus if needed
            testSectionControl().click();
            waitFor().isClickable();
        }
    }
%%end
```

## Make a link that will always wait before clicking
```
ComponentModel com.example.pmt.pages

%%start
    @Override
    public P click() {
        waitFor().isClickable()
        return super.click();
    }
%%end
```

## Make a button that will detect an error page and go back and re-click
This component is used on an InboxPage
when clicked will detect if it is on the InternalErrorPage, and navigate back to the InboxPage and attempt to re-click
it will only do this once and fail if it gets a second error page
```java
ComponentModel com.example.pmt.pages
import org.pagemodel.web.paths.PageFlow;

%%start
    @Override
    public P click() {
        return new PageFlow<P>(page.getContext(), InboxPage.class)
                .addPath(InternalErrorPage.class, p -> {
                    page.getContext().getDriver().navigate().back();
                    page.expectRedirect(InboxPage.class);
                    return clickAnd().expectRedirect((Class<P>)clickAction.getReturnPage().getClass());
                })
                .addPath((Class<P>)clickAction.getReturnPage().getClass(), p -> p)
                .testPaths();
    }
%%end
```

## Use javascript to set the value of a custom javascript control
```
DateField @@@DateComponent id "dateControl"
```
`DateComponent.pagemodel`
```
ComponentModel com.example.pmt.pages

%%start
    //Override the element tester sendKeys() to use javascript to set element value
    @Override
    public R sendKeys(CharSequence... keys) {
        page.testPage().testJavaScript("" 
                + "document.getElementById('dateControlHidden').value='" + 
                "keys".replace("'","\\'") +"';", 10);
        return getReturnObj();
    }
%%end
```
```java
    .testDateControl().sendKeys("03/16/2021")
```


## Wait for file download when visiting page
```java
.testSaveButton().click()
.doAction(page -> {
	Long start = System.currentTimeMillis();
	while (!downloadTmp.exists() && System.currentTimeMillis() < start + 60000) {
		Thread.sleep(2000);
	}
	Path from = Paths.get(downloadTmp.getAbsolutePath());
	Path to = Paths.get(smimeFile.getAbsolutePath());
	Files.move(from, to);
})
```

## Load mail html body in browser to test with page model

```java
.testMail()
.fetchMail(externalPop, MAIL_TIMEOUT, subject, newUser.getEmail())
.testHtmlBody().storeValue("html")
.closeMail()

//load html body in browser and test with MailPage.pagemodel
.testPage().navigateTo(HtmlUtils.htmlDataUri(webmailContext.loadString("html")), MailPage.class)
.testRegisterLink().click()
```

## Download and open html attachment
```java
File push = new File(new File(WebDriverFactory.DOWNLOAD_DIRECTORY), filename);

Mail.testMail(webmailContext)
		.composeMail()
		.from(sender.getEmail())
		.to(recipient.getEmail())
		.sendAndStore("mail", sender.getSmtp())

		.fetchSent(externalPop,30, ( sent, mail ) -> mail
				.testSubject().endsWith( sent.getSubject() )
				.testRecipientsTo().contains(recipient.getEmail()))
		.testAttachment("Attachment.html").byteContent(bytes -> {
			new FileOutputStream(push).write(bytes);
			return true;
		})
		.closeMail();

recipient.getLoginPage()
		.testPage().navigateTo(push.toURI().toString(), EmailAttachmentPage.class)
```