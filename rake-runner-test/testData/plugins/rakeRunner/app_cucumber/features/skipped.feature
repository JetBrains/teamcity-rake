Feature: Several skipped steps
  In order to test Cucumber formatter
  As a great and horrible tester
  I want to check that skipped features will be displayed correctly

  Scenario: first group of skipped steps
    When we have undefined step definition
    Then next steps should be skipped