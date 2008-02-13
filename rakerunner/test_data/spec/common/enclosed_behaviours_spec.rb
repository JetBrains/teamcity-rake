# Created by IntelliJ IDEA.
#
# @author: Roman.Chernyatchik
# @data: 13.02.2008

require "spec"

describe "Enclosed Behaviour" do

  it "should first:  [Enclosed Behaviour].should..." do
    true.should == false
  end

  describe "Level 1" do
    it "should first:  [Enclosed Behaviour].[Level 1].should..." do
      true.should == false
    end

    describe "Level 2" do
      it "should first:  [Enclosed Behaviour].[Level 1].[Level 2].should..." do
        true.should == false
      end

      describe "Level 3" do
        it "should first:  [Enclosed Behaviour].[Level 1].[Level 2].[Level 3].should..." do
          true.should == false
        end

        it "should last:  [Enclosed Behaviour].[Level 1].[Level 2].[Level 3].should..." do
          true.should == false
        end
      end

      it "should last:  [Enclosed Behaviour].[Level 1].[Level 2].[Level 3].should..." do
        true.should == false
      end
    end

    it "should last:  [Enclosed Behaviour].[Level 1].should..." do
      true.should == false
    end
  end

  it "should last:  [Enclosed Behaviour].should..." do
    true.should == false
  end
end