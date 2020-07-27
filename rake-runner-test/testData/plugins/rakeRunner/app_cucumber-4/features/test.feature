Feature: feature to test

  Scenario: one
    Given my test step
    And my test step with "param"


  Scenario Outline: two
    Given my test step
    And my test step with "<param>"
    Examples:
      | param  |
      | value'1' |
      | value\|n2 |
