class DateRange
  attr_accessor :start, :end

  def initialize(hash)
    return unless hash.respond_to? :fetch

    @start = hash.fetch('start', nil)
    @end   = hash.fetch('end',   "Present")
  end
end
