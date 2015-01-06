﻿using System.Collections.Generic;
using NUnit.Framework;
using OpenQA.Selenium.Environment;
using OpenQA.Selenium.Interactions;
using OpenQA.Selenium.Support.UI;
using System.Collections.ObjectModel;
using System;
using OpenQA.Selenium.Internal;

namespace OpenQA.Selenium.Support.PageObjects
{
    [TestFixture]
    public class PageFactoryBrowserTest : DriverTestFixture
    {
        //TODO: Move these to a standalone class when more tests rely on the server being up
        [TestFixtureSetUp]
        public void RunBeforeAnyTest()
        {
            EnvironmentManager.Instance.WebServer.Start();
        }
        
        [TestFixtureTearDown]
        public void RunAfterAnyTests()
        {
            EnvironmentManager.Instance.CloseCurrentDriver();
            EnvironmentManager.Instance.WebServer.Stop();
        }

        [Test]
        public void LooksUpAgainAfterPageNavigation()
        {
            driver.Url = xhtmlTestPage;
            var page = new Page();

            PageFactory.InitElements(driver, page);

            driver.Navigate().Refresh();

            Assert.True(page.formTestElement.Displayed);
        }

        [Test]
        public void CheckThatListIsFoundByIdOrName()
        {
            driver.Url = xhtmlTestPage;
            var page = new Page();

            PageFactory.InitElements(driver, page);
            Assert.GreaterOrEqual(1, page.someForm.Count);
        }

        [Test]
        public void CheckThatElementIsFoundByIdOrName()
        {
            driver.Url = xhtmlTestPage;
            var page = new Page();

            PageFactory.InitElements(driver, page);
            Assert.IsTrue(page.parent.Displayed);
        }

        [Test]
        public void ElementEqualityWorks()
        {
            driver.Url = xhtmlTestPage;
            var page = new Page();

            PageFactory.InitElements(driver, page);

            var expectedElement = driver.FindElement(By.Name("someForm"));
            var result = ((IWrapsElement)page.formTestElement).WrappedElement;

            Assert.True(result.Equals(expectedElement));
            Assert.AreEqual(expectedElement.GetHashCode(), result.GetHashCode());
        }

        [Test]
        public void UsesElementAsScriptArgument()
        {
            driver.Url = xhtmlTestPage;
            var page = new Page();

            PageFactory.InitElements(driver, page);

            var tagName = (string)((IJavaScriptExecutor)driver).ExecuteScript("return arguments[0].tagName", page.formTestElement);

            Assert.AreEqual("form", tagName.ToLower());
        }

        [Test]
        public void ShouldAllowPageFactoryElementToBeUsedInInteractions()
        {
            driver.Url = javascriptPage;
            var page = new PageFactoryBrowserTest.HoverPage();
            PageFactory.InitElements(driver, page);
            
            Actions actions = new Actions(driver);
            actions.MoveToElement(page.MenuLink).Perform();

            IWebElement item = driver.FindElement(By.Id("item1"));
            Assert.AreEqual("Item 1", item.Text);
        }

        [Test]
        public void ShouldFindMultipleElements()
        {
            driver.Url = xhtmlTestPage;
            var page = new PageFactoryBrowserTest.LinksPage();
            PageFactory.InitElements(driver, page);
            Assert.AreEqual(12, page.AllLinks.Count);
            Assert.AreEqual("Open new window", page.AllLinks[0].Text.Trim());
        }

        [Test]
        public void ShouldFindElementUsingSequence()
        {
            driver.Url = xhtmlTestPage;
            var page = new PageFactoryBrowserTest.Page();
            PageFactory.InitElements(driver, page);
            Assert.AreEqual("I'm a child", page.NestedElement.Text.Trim());
        }

        #region Page classes for tests
        #pragma warning disable 649 //We set fields through reflection, so expect an always-null warning

        private class Page
        {
            [FindsBy(How = How.Name, Using = "someForm")]
            public IWebElement formTestElement;

            public IList<IWebElement> someForm;
            public IWebElement parent;

            [FindsBySequence]
            [FindsBy(How = How.Id, Using = "parent", Priority = 0)]
            [FindsBy(How = How.Id, Using = "child", Priority = 1)]
            public IWebElement NestedElement;
        }

        private class HoverPage
        {
            [FindsBy(How=How.Id, Using="menu1")]
            public IWebElement MenuLink;
        }

        private class LinksPage
        {
            [FindsBy(How=How.TagName, Using="a")]
            public IList<IWebElement> AllLinks;
        }

        #pragma warning restore 649
        #endregion
    }
}
