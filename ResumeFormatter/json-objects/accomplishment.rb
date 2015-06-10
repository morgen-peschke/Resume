require_relative 'entry'

class Accomplishment < Entry
  attr_accessor :summary, :details

  def initialize(hash)
    return unless hash.respond_to? :fetch

    @summary = hash.fetch('summary', nil)
    @details = hash.fetch('details', nil)

    @details = Entry.convert @details
  end

end
