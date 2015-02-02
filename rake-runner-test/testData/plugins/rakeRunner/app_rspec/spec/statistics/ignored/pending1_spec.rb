# @author: Roman Chernyatchik
require "rspec"

describe "Pending" do

  it "should pending method" do
    pending("get sing of medved")
    medved.should say("preved")
  end

  if ::RSpec::Core::Version::STRING.split('.')[0] == '2'

    it "should pending block", :pending do
      pending("get sing of krevedko") do
        krevedko.should be("ya!")
      end
    end

    it "should be fixed pending" do
      pending("some") do
        expect(true).equal? true
      end
    end
  else # RSpec 3

    # Will be 'failed' because pending block will succeed
    pending "should be fixed pending" do
      expect(true).equal? true
    end

  end

end