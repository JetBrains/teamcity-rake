Given /^background$/ do
end

Then /^should pass$/ do
  expect(1).to eq(1)
end

Then /^should fail$/ do
  expect(1).to eq(2)
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

Given /^.*thing$/ do
end

Then /^I should see ".*" in the ".*" element$/ do
end
