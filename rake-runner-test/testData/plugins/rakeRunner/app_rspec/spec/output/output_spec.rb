# @author: Roman Chernyatchik
require "rspec"

describe "Output" do
  before do
    $stdout << "spec:before $stdout\n"
    STDOUT << "spec:before STDOUT\n"
    STDERR << "spec:before STDERR\n"
    $stderr << "spec:before $stderr\n"

    $stdout.flush
    STDOUT.flush
    $stderr.flush
    STDERR.flush
  end

  after do
    $stdout << "spec:after $stdout\n"
    STDOUT << "spec:after STDOUT\n"
    STDERR << "spec:after STDERR\n"
    $stderr << "spec:after $stderr\n"

    $stdout.flush
    STDOUT.flush
    $stderr.flush
    STDERR.flush
  end

  it "should pass" do
    $stdout << "spec:$stdout<<msg1"
    STDOUT << "\nspec:STDOUT<<msg2\n"
    $stderr << "spec:$stderr<<msg3\n"
    STDERR << "spec:STDERR<<msg4\n"

    $stdout.flush
    STDOUT.flush

    $stderr.flush
    STDERR.flush

    expect(true).to eq(true)
  end

  it "should fail" do
    $stdout << "spec:$stdout<<msg5"
    STDOUT << "\nspec:STDOUT<<msg6\n"
    $stderr << "spec:$stderr<<msg7\n"
    STDERR << "spec:STDERR<<msg8\n"

    $stdout.flush
    STDOUT.flush

    $stderr.flush
    STDERR.flush

    expect(true).to eq(false)
  end

  it "should error" do
    $stdout << "test:$stdout<<msg9"
    STDOUT << "\ntest:STDOUT<<msg10\n"
    $stderr << "test:$stderr<<msg11\n"
    STDERR << "test:STDERR<<msg12\n"

    $stdout.flush
    STDOUT.flush

    $stderr.flush
    STDERR.flush

    2 / 0
  end
end
