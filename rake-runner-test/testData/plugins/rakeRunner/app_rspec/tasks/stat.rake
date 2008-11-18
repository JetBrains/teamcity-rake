require "rake"
require "spec/rake/spectask"

########################################
namespace :stat do

  Spec::Rake::SpecTask.new(:general) do |t|
    t.spec_files = FileList['spec/statistics/general/**/*_spec.rb']
  end

  Spec::Rake::SpecTask.new(:passed) do |t|
    t.spec_files = FileList['spec/statistics/passed/**/*_spec.rb']
  end

  Spec::Rake::SpecTask.new(:failed) do |t|
    t.spec_files = FileList['spec/statistics/failed/**/*_spec.rb']
  end

  Spec::Rake::SpecTask.new(:error) do |t|
    t.spec_files = FileList['spec/statistics/error/**/*_spec.rb']
  end

  Spec::Rake::SpecTask.new(:ignored) do |t|
    t.spec_files = FileList['spec/statistics/ignored/**/*_spec.rb']
  end

  Spec::Rake::SpecTask.new(:compile_error) do |t|
    t.spec_files = FileList['spec/statistics/compile_error/**/*_spec.rb']
  end
end