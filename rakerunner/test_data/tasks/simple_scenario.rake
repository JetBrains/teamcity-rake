namespace :simple_sc do

  #directory "./"

   #task :dist => :dist_dir do
   desc "Main task"
   task :dist do
     puts File.dirname(".")
   end
end