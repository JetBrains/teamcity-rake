Given /^background$/ do
end

Then /^should pass$/ do
  1.should == 1
end

Then /^should fail$/ do
  1.should == 2
end

Then /^should be pending$/ do
  pending
end

Then /^should be error/ do
  2 / 0
end

Then /^next steps should be skipped$/ do
  true
end

When /^I do something wrong$/ do
  # do nothing
end
