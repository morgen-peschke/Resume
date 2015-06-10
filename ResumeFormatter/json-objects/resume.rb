require_relative 'contact'
require_relative 'skills'
require_relative 'project'
require_relative 'job'
require_relative 'education'

class Resume
  attr_accessor :name, :contact, :skills, :projects, :jobs, :education

  def initialize(hash)
    return unless hash.respond_to? :fetch

    @name      = hash.fetch('name',      nil)
    @contact   = hash.fetch('contact',   nil)
    @skills    = hash.fetch('skills',    nil)
    @projects  = hash.fetch('projects',  nil)
    @jobs      = hash.fetch('jobs',      nil)
    @education = hash.fetch('education', nil)

    @contact   = Contact.new @contact unless @contact.nil?
    @skills    = Skills.new  @skills  unless @skills.nil?
    @projects  = Entry.convert @projects, Project
    @jobs      = Entry.convert @jobs, Job
    @education = Entry.convert @education, Education
  end

end
